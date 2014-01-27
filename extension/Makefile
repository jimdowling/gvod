MODULE		= MozShim

# Change this to point at your Gecko SDK directory.
GECKO_SDK_PATH ?= $(HOME)/opt/gecko-sdk

XPIDL	       ?= $(GECKO_SDK_PATH)/bin/xpidl

XPIDLSRCS	= \
		gvodIChannel.idl \
		gvodITransport.idl \
		$(NULL)

GECKO_DEFINES  = -DMOZILLA_STRICT_API

GECKO_INCLUDES = -I$(GECKO_SDK_PATH)                    \
                 -I$(GECKO_SDK_PATH)/idl                \
                 -I$(GECKO_SDK_PATH)/include            \
                 -I$(GECKO_SDK_PATH)/xpcom/include      \
                 -I$(GECKO_SDK_PATH)/nspr/include       \
                 -I$(GECKO_SDK_PATH)/string/include     \
                 -I$(GECKO_SDK_PATH)/embedstring/include

%.h: %.idl
	$(XPIDL) -m header $(GECKO_INCLUDES) $<

%.xpt: %.idl
	$(XPIDL) -m typelib $(GECKO_INCLUDES) $<

build: $(MODULE).so

clean:
	rm $(MODULE).so
