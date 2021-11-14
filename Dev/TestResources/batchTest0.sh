#!/bin/bash

nc apps4sj.org 32421 < stage0.bin > test0.jpg
nc apps4sj.org 32421 < publish0.bin
#nc apps4sj.org 32421 < delete0.bin



