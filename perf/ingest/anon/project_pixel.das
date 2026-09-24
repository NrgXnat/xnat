// QA_REDACT: blanks a rectangle in every frame, exercising every codec the corpus carries.
version "6.1"
(0010,0010) := subject
(0010,0020) := session
(0012,0064) := "QA_REDACT rectangle applied"
alterPixels["rectangle", "l=10, t=10, r=60, b=60", "solid", "v=0"]
