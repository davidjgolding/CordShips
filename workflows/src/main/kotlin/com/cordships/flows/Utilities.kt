package com.cordships.flows

import net.corda.core.node.ServiceHub

/** Simple utility for getting the default notary */
fun ServiceHub.defaultNotary() = this.networkMapCache.notaryIdentities.first()