import pymupdf, os, shutil
src = r"C:\Users\Kirll\.dsh\attachments\v1\files\a1\a1eca7e0eeca2dd3914fffaf0be4e042fe5d3da10e5afcead75e1f42f9a58c3b\Fulfer_M_Iskusstvo_chtenia_po_litsu_2004.pdf"
out = r"C:\Users\Kirll\AndroidStudioProjects\physiognomy2\tools\hi"
os.makedirs(out, exist_ok=True)
doc = pymupdf.open(src)
for i in range(doc.page_count):
    pix = doc[i].get_pixmap(dpi=220, colorspace=pymupdf.csGRAY)
    pix.save(os.path.join(out, "p%03d.png" % (i+1)))
print("ok", doc.page_count)
