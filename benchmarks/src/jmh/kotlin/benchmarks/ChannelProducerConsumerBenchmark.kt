/*
 * Copyright 2016-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("unused")

package benchmarks

import benchmarks.common.*
import kotlinx.coroutines.selects.*
import org.openjdk.jmh.annotations.*
import java.lang.Integer.*
import java.util.concurrent.*


/**
 * Benchmark to measure channel algorithm performance in terms of average time per `send-receive` pair;
 * actually, it measures the time for a batch of such operations separated into the specified number of consumers/producers.
 * It uses different channels (rendezvous, buffered, unlimited; see [ChannelCreator]) and different dispatchers
 * (see [DispatcherCreator]). If the [_3_withSelect] property is set, it invokes `send` and
 * `receive` via [select], waiting on a local dummy channel simultaneously, simulating a "cancellation" channel.
 *
 * Please, be patient, this benchmark takes quite a lot of time to complete.
 */
@Warmup(iterations = 3, time = 500, timeUnit = TimeUnit.MICROSECONDS)
@Measurement(iterations = 5, time = 500, timeUnit = TimeUnit.MICROSECONDS)
@Fork(value = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
open class ChannelProducerConsumerBenchmark {
    @Param
    private var _0_dispatcher: DispatcherCreator = DispatcherCreator.FORK_JOIN

    @Param
    private var _1_channel: ChannelCreator = ChannelCreator.RENDEZVOUS

    @Param("0", "1000")
    private var _2_coroutines: Int = 0

    @Param("false", "true")
    private var _3_withSelect: Boolean = false

    @Param("1", "2", "4") // local machine
//    @Param("1", "2", "4", "8", "12") // local machine
//    @Param("1", "2", "4", "8", "16", "32", "64", "128", "144") // dasquad
//    @Param("1", "2", "4", "8", "16", "32", "64", "96") // Google Cloud
    private var _4_parallelism: Int = 0

    @Benchmark
    fun spmc() {
        if (_2_coroutines != 0) return
        val producers = max(1, _4_parallelism - 1)
        val consumers = 1
        val iteration = ChannelProducerConsumerBenchmarkIterationJMH(_0_dispatcher, _1_channel, _3_withSelect,
            _4_parallelism, producers, consumers)
        iteration.run()
    }

    @Benchmark
    fun mpmc() {
        val producers = if (_2_coroutines == 0) (_4_parallelism + 1) / 2 else _2_coroutines / 2
        val consumers = producers
        val iteration = ChannelProducerConsumerBenchmarkIterationJMH(_0_dispatcher, _1_channel, _3_withSelect,
            _4_parallelism, producers, consumers)
        iteration.run()
    }
}

private const val AVERAGE_WORK = 80
private const val APPROX_BATCH_SIZE = 100000

class ChannelProducerConsumerBenchmarkIterationJMH(dispatcherCreator: DispatcherCreator, channelCreator: ChannelCreator,
                                                   withSelect: Boolean, parallelism: Int, producers: Int, consumers: Int)
    : ChannelProducerConsumerBenchmarkIteration(channelCreator, withSelect, producers, consumers, dispatcherCreator,  parallelism, APPROX_BATCH_SIZE) {
    override fun doProducerWork(id: Int) = doGeomDistrWork(AVERAGE_WORK)
    override fun doConsumerWork(id: Int) = doGeomDistrWork(AVERAGE_WORK)
}