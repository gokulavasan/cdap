/*
 * Copyright (c) 2013, Continuuity Inc
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms,
 * with or without modification, are not permitted
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.continuuity.testsuite.purchaseanalytics;

import com.continuuity.api.flow.Flow;
import com.continuuity.api.flow.FlowSpecification;

/**
 * Reads transactions from stream, process purchase, customer, product, inventory.
 */
public class PurchaseAnalyticsFlow implements Flow {

  @Override
  public FlowSpecification configure() {
    return FlowSpecification.Builder.with()
      .setName("PurchaseAnalyticsFlow")
      .setDescription("Reads transactions from stream, process purchase, customer, product, inventory.")
      .withFlowlets()
      .add("reader", new StreamReaderFlowlet())
      .add("purchase-collector", new PurchaseStoreFlowlet())
      .add("product-collector", new ProductStoreFlowlet())
      .add("customer-collector", new CustomerStoreFlowlet())
      .connect()
      .fromStream("transactionStream").to("reader")
      .from("reader").to("purchase-collector")
      .from("reader").to("product-collector")
      .from("reader").to("customer-collector")
      .build();
  }
}