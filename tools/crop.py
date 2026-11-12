from PIL import Image
im = Image.open(r"C:\Users\Kirll\AndroidStudioProjects\physiognomy2\shots\f1_brows.png")
# crop icon area of first card: around x 90-220, y 1100-1190
box = (70, 1090, 260, 1200)
im.crop(box).resize((box[2]-box[0])*4//1 and ((box[2]-box[0])*4, (box[3]-box[1])*4), Image.NEAREST).save(r"C:\Users\Kirll\AndroidStudioProjects\physiognomy2\shots\crop_brows.png")
print("ok")
