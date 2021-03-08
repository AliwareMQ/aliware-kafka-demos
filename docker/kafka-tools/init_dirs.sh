#!/bin/bash 


work_dir="/home/admin"
log_dir="/usr/local/test/log"

if [ ! -z $WORK_DIR ]; then
    work_dir=$WORK_DIR
fi

init_dirs=("$log_dir" "$work_dir/kafka-tools" "$work_dir/aliware-kafka-demos" "$work_dir/go/pkg/mod/cache")

for init_dir in ${init_dirs[@]}
do
    if [ ! -d $init_dir ]; then
        mkdir -p $init_dir
    fi
done



