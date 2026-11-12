from pypdf import PdfReader
src = r"C:\Users\Kirll\.dsh\attachments\v1\files\a1\a1eca7e0eeca2dd3914fffaf0be4e042fe5d3da10e5afcead75e1f42f9a58c3b\Fulfer_M_Iskusstvo_chtenia_po_litsu_2004.pdf"
r = PdfReader(src)
for i in range(3):
    p = r.pages[i]
    print("page", i+1, "images", len(p.images), [ (im.name, len(im.data)) for im in p.images ])
