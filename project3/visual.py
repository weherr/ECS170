
import sys
import os
import numpy as np
import matplotlib.pyplot as plt
 
## for showing all pictures in a given directory one at a time
## syntax is: python imgShow.py [directory]
if os.path.isdir(sys.argv[1]):
  dir = sys.argv[1]
  os.chdir(dir)
 
  for file in os.listdir(os.getcwd()):
    if file.endswith(".txt"):
      print file
      img = np.loadtxt(file, delimiter=" ")
      img = img.reshape((120, 128))
      plt.imshow(img)
      plt.set_cmap("gray")
      plt.show()
      
## for showing just one picture
## syntax is: python imgShow.py [pictureFileName]
elif os.path.isfile(sys.argv[1]):
  file = sys.argv[1]
  img = np.loadtxt(file, delimiter=" ")
  img = img.reshape((120, 128))
  plt.imshow(img)
  plt.set_cmap("gray")
  plt.show()
  
## for inserting a string before the ".txt" in a file name to ALL .txt  
##    files in a directory
## I used this to rename all my Male and Female files so my .java 
##    program could tell what the answers were based on file names.
## syntax is: python imgShow.py rename [directory] [stringToInsert]
elif sys.argv[1] == "rename":
  dir = sys.argv[2]
  os.chdir(dir)
  string = sys.argv[3]
  for file in os.listdir(os.getcwd()):
    if file.endswith(".txt"):
      newName = file.replace(".txt", string + ".txt", 1)
      os.rename(file, newName)
      