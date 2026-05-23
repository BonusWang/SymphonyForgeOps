package com.symphonyforgeops.api.domain.model;

import java.util.List;

public record ReviewDashboard(
        List<ReviewFinding> findings,
        List<HumanDecision> humanDecisions
) {
}
