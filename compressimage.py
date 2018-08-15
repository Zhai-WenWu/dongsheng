# -*- coding: UTF-8 -*-
import os

# 获取指定路径下所有指定后缀的文件
# dir 指定路径
# ext 指定后缀，链表&不需要带点 或者不指定。例子：['xml', 'java']
def GetFileFromThisRootDir(dir, ext = None):
    allfiles = []
    needExtFilter = (ext != None)
    for root,dirs,files in os.walk(dir):
        for filespath in files:
            filepath = os.path.join(root, filespath)
            extension = os.path.splitext(filepath)[1][1:]
            if needExtFilter and extension == ext in ext:
                allfiles.append(filepath)
    return allfiles


def list_all_files(rootdir):
    import os
    _files = []
    list = os.listdir(rootdir) #列出文件夹下所有的目录与文件
    for i in range(0,len(list)):
           path = os.path.join(rootdir,list[i])
           if os.path.isdir(path):
              _files.extend(list_all_files(path))
           if os.path.isfile(path):
              _files.append(path)

    return _files

if __name__ == '__main__':
    PngquantExe="/Users/yulekwok/Documents/macDev/pngquant/pngquant"
    srcDir = os.path.dirname(os.path.realpath(__file__))
    # print("srcDir is ",srcDir)
    # srcDir = "/Users/yulekwok/Desktop/minfangPhoto2"
    type_name =  'png'
    # imgFiles=GetFileFromThisRootDir(srcDir, name)
    suffix="_png_quan_test.png"
    all_files =  list_all_files(srcDir)
    for filepath in all_files:
       extension = os.path.splitext(filepath)[1][1:]
       if extension.lower() ==  type_name:
           # print("oldfile", filepath)
           cmd = "\"" + PngquantExe + "\"" + " --ext " + suffix + " --force --speed=3 "+ filepath.replace(" ","\ ")
           # cmd = "\"" + PngquantExe + "\"" + " --quality=0-100 " + f
           os.system(cmd)
           newfile=filepath.replace("." + extension, suffix)
           os.remove(filepath)
           # print("newfile",newfile)
           os.rename(newfile, filepath)
    print("############# compress image is OK #############")


    # for f in imgFiles:
    #     print("file",f)
    #     cmd = "\"" + PngquantExe + "\"" + " --ext " + suffix + " --force --speed=3 "+ f
    #     # cmd = "\"" + PngquantExe + "\"" + " --quality=0-100 " + f
    #
    #     os.system(cmd)
    #
    #     newfile=f.replace(".PNG", suffix)
    #     # os.remove(f)
    #     print("newfile",newfile)
    #     # os.rename(newfile, f)
