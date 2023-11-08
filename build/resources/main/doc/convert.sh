#!/usr/bin/env bash
pandoc -s -o building.html building.md --metadata pagetitle="Building Jalview from Source" --toc -H github.css
