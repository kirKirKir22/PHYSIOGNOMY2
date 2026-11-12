import sys, io
from pypdf import PdfReader
src = r"C:\Users\Kirll\.dsh\attachments\v1\files\a1\a1eca7e0eeca2dd3914fffaf0be4e042fe5d3da10e5afcead75e1f42f9a58c3b\Fulfer_M_Iskusstvo_chtenia_po_litsu_2004.pdf"
out = r"C:\Users\Kirll\AndroidStudioProjects\physiognomy2\tools\fulfer.txt"
import os
os.makedirs(os.path.dirname(out), exist_ok=True)
r = PdfReader(src)
print("pages", len(r.pages))
with io.open(out, "w", encoding="utf-8") as f:
    for i, p in enumerate(r.pages):
        try:
            t = p.extract_text() or ""
        except Exception as e:
            t = ""
        f.write("\n\n===== PAGE %d =====\n" % (i+1))
        f.write(t)
print("done")
