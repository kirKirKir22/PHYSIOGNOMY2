import pymupdf, os
src = r"C:\Users\Kirll\.dsh\attachments\v1\files\a1\a1eca7e0eeca2dd3914fffaf0be4e042fe5d3da10e5afcead75e1f42f9a58c3b\Fulfer_M_Iskusstvo_chtenia_po_litsu_2004.pdf"
out = r"C:\Users\Kirll\AndroidStudioProjects\physiognomy2\tools\pages"
os.makedirs(out, exist_ok=True)
doc = pymupdf.open(src)
print("pages", doc.page_count)
for i in range(doc.page_count):
    p = doc[i]
    pix = p.get_pixmap(dpi=110)
    pix.save(os.path.join(out, "p%03d.png" % (i+1)))
print("rendered")
