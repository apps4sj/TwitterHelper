#!/bin/bash

nc apps4sj.org 32421 < stage1.bin > test1.jpg
nc apps4sj.org 32421 < publish1.bin
#nc apps4sj.org 32421 < delete1.bin


