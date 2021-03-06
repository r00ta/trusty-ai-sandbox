/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.redhat.developer.counterfactual.metrics;

import com.redhat.developer.counterfactual.entities.CreditCardApprovalEntity;
import com.redhat.developer.counterfactual.solutions.Approval;
import org.optaplanner.benchmark.api.PlannerBenchmark;
import org.optaplanner.benchmark.api.PlannerBenchmarkFactory;

import javax.xml.bind.JAXBException;

public class Benchmark {

  public static void main(String[] args) throws JAXBException {

    // create dataset
    Approval solution = new Approval();

    Facts.input = new CreditCardApprovalEntity(30, 10000, 0, 100, false, false, false);

    PlannerBenchmarkFactory benchmarkFactory =
        PlannerBenchmarkFactory.createFromFreemarkerXmlResource("BenchmarkConfig.ftl");

    PlannerBenchmark benchmark = benchmarkFactory.buildPlannerBenchmark(solution);
    benchmark.benchmarkAndShowReportInBrowser();
  }
}
