#!/bin/bash
echo "Runscript 0.1"
python -m SimpleHTTPServer 8000 &
../Utils/miniircd --debug &
