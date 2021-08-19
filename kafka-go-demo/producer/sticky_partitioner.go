package main

import (
	"github.com/Shopify/sarama"
	"hash"
	"hash/fnv"
)


//implement the function pointer
// type PartitionerConstructor func(topic string) Partitioner
func NewStickyPartitioner(topic string) sarama.Partitioner {
	p := new(stickyPartitioner)
	p.hasher = fnv.New32a()
	p.referenceAbs = false
	p.stickSize = 32 * 1024
	p.totalSize = 0
	return p
}

type stickyPartitioner struct {
	hasher       hash.Hash32
	referenceAbs bool
	stickSize int32
	totalSize int32
}

func (p *stickyPartitioner) Partition(message *sarama.ProducerMessage, numPartitions int32) (int32, error) {
	if message.Key == nil {
		if message.Value != nil {
			p.totalSize += int32(message.Value.Length())
		}
		//prevent overflow
		if p.totalSize > p.stickSize * numPartitions {
			p.totalSize = p.totalSize - p.stickSize * numPartitions
		}
		//double check overflow
		if p.totalSize < 0 {
			p.totalSize = 0
		}
		return (p.totalSize / p.stickSize) % numPartitions, nil
	}
	bytes, err := message.Key.Encode()
	if err != nil {
		return -1, err
	}
	p.hasher.Reset()
	_, err = p.hasher.Write(bytes)
	if err != nil {
		return -1, err
	}
	var partition int32
	// Turns out we were doing our absolute value in a subtly different way from the upstream
	// implementation, but now we need to maintain backwards compat for people who started using
	// the old version; if referenceAbs is set we are compatible with the reference java client
	// but not past Sarama versions
	if p.referenceAbs {
		partition = (int32(p.hasher.Sum32()) & 0x7fffffff) % numPartitions
	} else {
		partition = int32(p.hasher.Sum32()) % numPartitions
		if partition < 0 {
			partition = -partition
		}
	}
	return partition, nil
}

func (p *stickyPartitioner) RequiresConsistency() bool {
	return true
}

func (p *stickyPartitioner) MessageRequiresConsistency(message *sarama.ProducerMessage) bool {
	return message.Key != nil
}
