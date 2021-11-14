#!/bin/bash

nc apps4sj.org 32421 < stage3.bin > test3.jpg
nc apps4sj.org 32421 < publish3.bin
#nc apps4sj.org 32421 < delete3.bin



