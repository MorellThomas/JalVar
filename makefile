DEST_PATH := ./
CONFIG_PATH := ./JalVar/
ifeq ($(shell uname),Darwin)
DEST_PATH := /Applications/
CONFIG_PATH := /Users/$(SUDO_USER)/
endif
ifeq ($(shell uname),Linux)
DEST_PATH := /usr/bin/
CONFIG_PATH := /home/$(SUDO_USER)/JalVar/
endif

BUILD_PATH := build/libs/
JAR_NAME := jalview-all-2.11.3.0-j11.jar
BUILD_JAR := $(BUILD_PATH)$(JAR_NAME)
INSTALL_JAR := $(DEST_PATH)JalVar.jar
#REF_FILE := $(CONFIG_PATH)TTN.ref
REF_STRUCS := $(CONFIG_PATH)STRUCS
REF_VCF := $(CONFIG_PATH)TTN-ClinVar-SNVs.vcf $(CONFIG_PATH)OBSCN-ClinVar-SNVs.vcf
REF_GENE := $(CONFIG_PATH)TTN-OBSCN-gene-sequence.fa $(CONFIG_PATH)TTN-gene-sequence.fa
REF_PROTEIN := $(CONFIG_PATH)TTN-OBSCN-protein-sequence.fa $(CONFIG_PATH)TTN-protein-sequence.fa
REF_MSA := $(CONFIG_PATH)TTN-PaSiMap-group-C_OBSCN.fa $(CONFIG_PATH)TTN-Ig-PaSiMap-group-C.fa

REFS := $(REF_STRUCS) $(REF_VCF) $(REF_GENE) $(REF_PROTEIN) $(REF_MSA) #$(REF_FILE)

.DELETE_ON_ERROR :
.PHONY : install clean uninstall

$(BUILD_JAR) : 
	gradle shadowJar

install : $(BUILD_JAR) $(REFS)
	mkdir -p $(DEST_PATH)
	cp $(BUILD_JAR) $(INSTALL_JAR)
	chown $(SUDO_USER) $(CONFIG_PATH)

$(REFS) :
	mkdir -p $(CONFIG_PATH)
	cp -r sample/$(notdir $@) $@

clean : 
	-rm $(BUILD_JAR)

uninstall :
	-rm $(INSTALL_JAR) $(REF_VCF) $(REF_MSA) #$(REF_FILE) 
	-rm -rf $(REF_STRUCS)

