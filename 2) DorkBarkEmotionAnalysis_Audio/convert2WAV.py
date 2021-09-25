import os

#convert m4a => wav using ffmpeg in bulk

def convert(fileDir, outputDir, label):
    #getting wav files in directory
    files = [f for f in os.listdir(fileDir) if ".wav" in f]
    
    #original file in dogBarks downloaded are m4a file format
    #start converting m4a => wav
    count = 0
    for file in files:
        outFile = "%s_conv%d.wav" % (label, count)
        cmd = """ffmpeg -i %s%s -acodec pcm_u8 -ac 1 %s%s""" % (fileDir, file, outputDir, outFile)
        os.system(cmd)
        print("[*] Conv Complete: %s" % (file))
        count+=1
    
def main():
    label = ['bow-wow', 'growling', 'howl']
    oriDirLoc = "dogBarks"
    outputDir = "converted"

    #converting all audio into wav in bulk
    for l in label:
        fileDir = "%s/%s/" % (oriDirLoc, l)                
        outDir = "%s/%s/" % (outputDir, l)
        convert(fileDir, outDir, l)
        print("[*] Conv Label Complete: %s " % (l))

if __name__ == "__main__":
    main()