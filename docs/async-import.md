# Importing DICOM in the background

A DICOM import through `POST /data/services/import` normally keeps the request open until XNAT has
received the files, and built and archived the sessions if the request asks for that. For a large
zip, that can mean an HTTP request that runs for many minutes.

XNAT can instead run the import in the background and report its progress through the event
tracking API. The Compressed Uploader and the image uploader work this way, and their progress is
what the Activity tab shows.

- **XNAT 1.11 and later:** send the file with `async=true`, then poll.
- **XNAT 1.8 to 1.10:** upload the file to your user cache, start the import from there, poll, and
  delete the file. This still works in 1.11, with a few differences noted below.

## XNAT 1.11 and later

### 1. Send the file with `async=true`

```http
POST /data/services/import?async=true&inbody=true&import-handler=DICOM-zip&project={project}
Content-Type: application/zip

<the zip or tar file>
```

- Send the file as for any import: in the body with `inbody=true`, or as a multipart form. A file
  already in your user cache can be named with `src` instead.
- In the body, the content type tells XNAT what the file is: `application/zip` for a zip, and
  `application/x-gtar` for a `.tar.gz` or `.tgz`. A multipart form sends the file's name instead.
- `async=true` applies to an imported file. An inbox import (`import-handler=inbox`) sends none,
  and already answers right away: `202 Accepted`, with the URL of its own status, `/xapi/dicom/{id}`,
  in the body and the `Location` header.
- Every other parameter works as it does for any import: `project`, `subject`, `session`,
  `overwrite`, `action`, `auto-archive`, `Ignore-Unparsable`, and so on. See the
  [Image Session Import Service API](https://wiki.xnat.org/xnat-api/image-session-import-service-api).
- To choose the tracking key yourself, add `http-session-listener={key}`. Otherwise XNAT makes one.

XNAT stores the file, queues the import, and responds `202 Accepted`. The response body is the
tracking key, and the `Location` header is its URL, `/xapi/event_tracking/{key}`.

### 2. Poll the import's progress

See [Polling an import](#polling-an-import). XNAT deletes the stored file once the import has run.

## XNAT 1.8 to 1.10

### 1. Upload the file to your user cache

```http
PUT /data/user/cache/resources/{folder}/files/{file}?inbody=true
Content-Type: application/zip

<the zip or tar file>
```

- `{folder}` is any folder name. Use a new one for each upload, so uploads don't collide.
- `{file}` is the file's name. Keep it to letters, digits, dots, dashes, and underscores, since the
  same path is passed to the import in the next step.
- A multipart form with a `file` field works too. See the
  [User Resources API](https://wiki.xnat.org/xnat-api/user-resources-api).

### 2. Start the import

```http
POST /data/services/import?src=/user/cache/resources/{folder}/files/{file}&http-session-listener={key}&import-handler=DICOM-zip&action=commit&project={project}
```

- `src` is the path from step 1, starting at `/user/cache`, without `/data`.
- `http-session-listener` is the key you'll poll. Use a value that's unique for each import, such
  as a UUID.
- The import runs in the background only when the request has both `src` and
  `http-session-listener`.
- Include `action=commit`, as the Compressed Uploader does: it builds the sessions as part of the
  import, and add `auto-archive=true` to archive them too. Without it, XNAT before 1.11 never
  reports the import's end, and polling never finishes.

Once the import is queued, XNAT responds with the text `Submitted for prearchival`: `200` before
XNAT 1.11, and `202 Accepted` from 1.11, with the tracking URL in the `Location` header.

### 3. Poll the import's progress

See [Polling an import](#polling-an-import).

### 4. Delete the file

XNAT before 1.11 leaves the file in your user cache. Once the import has finished, delete it:

```http
DELETE /data/user/cache/resources/{folder}/files/{file}
```

XNAT 1.11 deletes the file once its import has run, so there this answers `404`.

## Polling an import

```http
GET /xapi/event_tracking/{key}
```

```json
{
  "key": "{key}",
  "payload": "{\"entryList\":[{\"status\":\"InProgress\",\"eventTime\":1793729145789,\"message\":\"Importing sessions to the prearchive\"}]}",
  "succeeded": null,
  "finalMessage": null
}
```

- `succeeded` is `null` while the import is running, then `true` or `false`.
- `payload` is a JSON document in a string. Its `entryList` holds the import's progress messages,
  oldest first. Each has a `status` (`Waiting`, `InProgress`, `Warning`, `Failed`, or `Completed`),
  an `eventTime` in milliseconds since the epoch, and a `message`.
- When `succeeded` is `true`, `finalMessage` names the sessions a DICOM import produced, as
  `Prearchive:` or `Archive:` followed by their URIs separated by semicolons, for example
  `Prearchive:/prearchive/projects/P1/20261103_182111029/S1`. Add `/data` to the front of each
  URI to request it. Other import handlers word it their own way: a XAR import's is the session's
  URI alone.
- When `succeeded` is `false`, `finalMessage` gives the reason. It also names any sessions that
  were archived before the failure.
- Only the user who started the import can read its key. Before XNAT 1.11, the response is `404`
  until the import reports its first step, which can wait in a queue, so allow for that.

Without `action=commit`, a DICOM-zip import is done once its files are in the prearchive. XNAT
builds each session after it has been idle for the session idle time, and archives it if the
project archives automatically.

With `action=commit`, XNAT skips any file in the zip that isn't DICOM. Add `Ignore-Unparsable=false`
to fail the import instead.

## Example (XNAT 1.11)

```python
import time

import requests

xnat = "https://xnat.example.org"
session = requests.Session()
session.auth = ("user", "password")

with open("study.zip", "rb") as fh:
    response = session.post(f"{xnat}/data/services/import",
                            params={"async": "true", "inbody": "true", "import-handler": "DICOM-zip",
                                    "project": "P1", "action": "commit", "auto-archive": "true"},
                            data=fh, headers={"Content-Type": "application/zip"})
response.raise_for_status()
status_url = response.headers["Location"]

while True:
    status = session.get(status_url).json()
    if status["succeeded"] is not None:
        break
    time.sleep(5)

print("succeeded" if status["succeeded"] else "failed", status["finalMessage"])
```
