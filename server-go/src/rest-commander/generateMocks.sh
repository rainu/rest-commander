#!/bin/bash

###
# FUNCTIONS
###

generateMock() {
    SRC=$1
    INTERFACES=$2
    PKG=$(dirname $SRC)
    DST=$PKG/mock_$(basename $SRC)

    mkdir -p $(dirname $DST)
    mockgen -source ./$SRC -destination ./$DST -package $(basename $PKG) $INTERFACES
    gofmt -w ./$DST
}

generateVendorMock() {
    PKG=$1
    INTERFACES=$2
    DST=mocks/$PKG/mock_$(basename $PKG).go

    mkdir -p $(dirname $DST)
    mockgen -destination ./$DST -package $(basename $PKG) $PKG $INTERFACES
    gofmt -w ./$DST
}

###
# Main
###

generateVendorMock net/http Handler,ResponseWriter
generateMock controller/auth_handler.go
generateMock controller/process_handler.go
generateMock store/linux_user_store.go
generateMock store/user.go
generateMock store/token.go