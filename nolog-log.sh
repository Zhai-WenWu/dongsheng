#!/bin/sh

#  Script.sh
#  Pods
#
#  Created by yule Kwok on 2018/4/12.
#


#! /bin/bash
function swift(){
#echo "hello , you are calling the function"
var="$1"
#echo var = $var
FileExtension=${var##*.}
#echo 文件后缀是 $FileExtension
# &&
if [[ "$FileExtension" == "java" ]]
then
haveFlag=`grep 'YLKLog\.i(' $var | wc -l`
if [ $haveFlag -ne 0 ]
then
echo "更改的文件是" $var
sed -i "" 's/\/\/YLKLog\.i(/ Log\.i(/g' $var
fi
fi
}



function read_dir(){
for file in `ls $1`
do
if test -f $file
then
#echo $file 是文件
swift $file
else
#echo $file 是目录
read_dir $1"/"$file
fi
done
}


set -e
BUILD_DIR=`pwd`
WORKING_DIR="${BUILD_DIR}"
#echo $WORKING_DIR
#mdfind -onlyin $WORKING_DIR "*.swift"
#!bin/sh
echo "---------------//Log -> Log 开始替换 ---------------"
read_dir $WORKING_DIR
echo "---------------//Log -> Log 替换结束 ---------------"

# swift $WORKING_DIR




