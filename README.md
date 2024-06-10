# XNAT

Future source of XNAT. Currently an under-construction monorepo.

## Background
### What even is this?
Right now this is nothing. The goal at the end is for this repo to contain all of XNAT's code,
in a single place with a single build.

### What do we have now?
Currently, XNAT's build process is seemingly simple. You clone the [`xnat-web` repo](https://bitbucket.org/xnatdev/xnat-web)
and run the build command

    ./gradlew war

You'll get an XNAT war. Easy, right?

This surface simplicity hides a lot of complexity. A ton of the code that gets built into that war
isn't in `xnat-web` at all, it's in libraries. And I don't just mean third-party dependencies,
I mean a whole zoo of small repositories within [xnatdev], each of which
must be built and deployed first before `xnat-web` can build successfully. If you need to make any changes
to that code, you need to clone the repo, load it into your IDE or whatever, make your changes,
build, test, merge, etc. before you finally deploy whatever you've worked on to a place where your
`xnat-web` build can pull it in. 

It can be quite challenging if you need to make coordinated changes across multiple of these repos.
You have to update all their versions in all the repos to ensure that they all are built against the correct
versions of everything. This is tricky enough to do locally, where you are building everything by hand and 
"deploying" the jars to a local store on your own machine. It is more difficult when you want to perform
the build on a different machine, which means all the libraries must be published somewhere accessible,
or the build must know which branches of all the repos to use.

This chaos is (somewhat) kept in order using a [parent][] repo where all the versions are kept. All the
libraries and `xnat-web` point to this common `parent` as the source of their dependency versions.

Oh, also some of the builds use Gradle and others use Maven. For the most part that doesn't really matter,
since if we're pulling a dependency from a published jar the build tool used to construct it is immaterial.
Except for parent, which doesn't actually produce a jar; its only artifact is the maven `pom.xml` file
containing the dependency versions.

### What do we want?
A way to tame this chaos is what we call the monorepo: a single repository containing XNAT's code from
`xnat-web` and all (...or mostly all) of the upstream libraries used to build XNAT's specific parts.
And there should be a single build system that is capable of compiling, packaging, and deploying everything.

This should be much easier to develop and build. Any change made anywhere in the code would be built
into some library jar where it "lives" but also into the downstream libraries and XNAT itself. The mental
and operational load of simply knowing where everything is and how to build it all would be reduced by
multiple orders of magnitude.

Ideally we would be able to pull in all the code from the various repos without losing their git history.
As in, we don't simply want to copy the files from one repo to another and check them in as they are right now.

### Why is it hard?
The primary obstacle is actually constructing the build that is capable of doing all this. We can no longer
keep all the mixed gradle/maven builds—those two don't interoperate at build time—we have to put everything
into one build system.

Converting all the builds is a daunting task. It seems implausible that we can do it all in one shot.
So we need a way to incrementally convert the standalone repos into the monorepo while keeping everything
functional along the way. But what does this partial / incremental monorepo look like? How does it
build and what is in it?

## Planning
### What build system should we use?
It is possible in principle for both maven and gradle to build a monorepo project. But we have to pick one
and convert everything that isn't currently in that system. In principle it is possible to convert in
either direction. However, my current subjective impression is that it is easier to convert a maven build into
a gradle build than a gradle build into a maven build. Which leads to a conclusion:

**The monorepo will be built with gradle**

### Where will all the repos go?
In the end everything will live here, in this repo.
Bringing in the source repos whilst maintaining their git history is actually not that hard.
It uses the `git subtree` command.

First, add the repository that you're bringing in as a "remote". For this example I'll use
[xnat-web][].

    git remote add xnat-web git@bitbucket.org:xnatdev/xnat-web.git

Fetch the content of the remote. 

    git fetch xnat-web

Add the content of the remote repository into this repository in a subdirectory. For this
task we will use the `git subtree` command (see [git-subtree][]).

    git subtree add -P xnat-web xnat-web develop

With this command, we merge the `develop` branch of `xnat-web` (along with all its commit history)
into our repo in the subdirectory `xnat-web`.

### How will the build work?
This is still a bit unclear. It seems like we will be using the concept of a gradle [composite build][],
which is a top-level build that can include other builds. But maybe that is not correct. Maybe we should
be structuring this as a [multiproject build][]. The two are similar. A composite build seems to keep
a bit more distance between the projects, because they are run entirely separately and don't share any
configuration, where a multiproject build runs one "build" that will build the projects with shared
configuration.

I'm thinking that a composite build would be easier to bootstrap, since we already have separate builds,
but what we actually want is a multiproject build. We do have a lot of shared state and shared configuration
and we should take advantage of that. That's another conclusion:

**The monorepo will be structured as a gradle multiproject build**

That doesn't actually answer the question, though. How will the build _work_?

We need to have a `settings.gradle` file at the root level that will `include` the builds from the
subprojects/subdirectories. And then from that top-level build we will somehow build all the subprojects.

To run some gradle task `<task>` in a subproject, say `web`, you can run

    ./gradlew web:<task>

For example, to build the XNAT war, we run

    ./gradlew web:war

[composite build]: https://docs.gradle.org/current/userguide/composite_builds.html
[multiproject build]: https://docs.gradle.org/current/userguide/multi_project_builds.html
[git-subtree]: https://github.com/git/git/blob/master/contrib/subtree/git-subtree.txt
[xnat-web]: https://bitbucket.org/xnatdev/xnat-web
