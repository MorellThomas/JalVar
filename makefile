DEST_PATH := ./
ifeq ($(shell uname),Darwin)
DEST_PATH := /Applications/
endif
ifeq ($(shell uname),Linux)
DEST_PATH := /usr/bin/
endif

BUILD_PATH := build/libs/
JAR_NAME := jalview-all-TEST-j11.jar
BUILD_JAR := $(BUILD_PATH)$(JAR_NAME)
INSTALL_JAR := $(DEST_PATH)jalviewSNV.jar
REF_FILE := $(DEST_PATH).TTn.ref

.DELETE_ON_ERROR :
.PHONY : install clean uninstall

$(BUILD_JAR) : 
	gradle shadowJar

install : $(BUILD_JAR) $(REF_FILE)
	mv $(BUILD_JAR) $(INSTALL_JAR)

# cp ref in install, have creation of ref here
$(REF_FILE) : 
	mkdir -p $(DEST_PATH)
	cp sample/TTN.ref $(REF_FILE)

clean : 
	rm $(BUILD_JAR) $(REF_FILE) #$(JAR_NAME)

uninstall :
	rm $(INSTALL_JAR) $(REF_FILE)

