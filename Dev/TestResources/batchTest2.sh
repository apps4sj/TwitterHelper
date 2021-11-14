#!/bin/bash

nc apps4sj.org 32421 < stage2.bin > test2.jpg
nc apps4sj.org 32421 < publish2.bin
#nc apps4sj.org 32421 < delete2.bin



