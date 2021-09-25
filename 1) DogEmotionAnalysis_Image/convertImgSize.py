from PIL import Image
import os, glob

dir = 'sick'

imgDir = "downloads/"+dir
new_width  = 224
new_height = 224

data_path = os.path.join(imgDir, '*g')
files = glob.glob(data_path)

i=0

for file in files:
	try:
		print('[+] Converting: '+file)
		img = Image.open(file) # image extension *.png,*.jpg
		img = img.resize((new_width, new_height), Image.ANTIALIAS)	
		img.save('convOut\{:s}\{:s}_{:04d}.jpg'.format(dir, dir, i)) # format may what you want *.png, *jpg, *.gif
		i+=1
	except:
		continue