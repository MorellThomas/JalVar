DEST_PATH := ./
CONFIG_PATH := ./
ifeq ($(shell uname),Darwin)
DEST_PATH := /Applications/
CONFIG_PATH := ~/.config/JalviewSNV/
endif
ifeq ($(shell uname),Linux)
DEST_PATH := /usr/bin/
CONFIG_PATH := ~/.config/JalviewSNV/
endif

BUILD_PATH := build/libs/
JAR_NAME := jalview-all-2.11.3.0-j11.jar
BUILD_JAR := $(BUILD_PATH)$(JAR_NAME)
INSTALL_JAR := $(DEST_PATH)jalviewSNV.jar
REF_FILE := $(CONFIG_PATH)TTN.ref
REF_STRUCS := $(CONFIG_PATH)STRUCS
REF_VCF := $(CONFIG_PATH)TTN.vcf

.DELETE_ON_ERROR :
.PHONY : install clean uninstall

$(BUILD_JAR) : 
	gradle shadowJar

install : $(BUILD_JAR) $(REF_FILE) $(REF_STRUCS) $(REF_VCF)
	mkdir -p $(DEST_PATH)
	mv $(BUILD_JAR) $(INSTALL_JAR)
	chown $(SUDO_USER) $(CONFIG_PATH)

# cp ref in install, have creation of ref here
$(REF_FILE) : 
	mkdir -p $(CONFIG_PATH)
	cp sample/TTN.ref $(REF_FILE)

$(REF_STRUCS) :
	mkdir -p $(CONFIG_PATH)
	cp -r sample/STRUCS $(REF_STRUCS)

$(REF_VCF) :
	mkdir -p $(CONFIG_PATH)
	cp sample/TTN.vcf $(REF_VCF)

clean : 
	rm $(BUILD_JAR)

uninstall :
	rm $(INSTALL_JAR) $(REF_FILE) $(REF_VCF)
	rm -rf $(REF_STRUCS)

