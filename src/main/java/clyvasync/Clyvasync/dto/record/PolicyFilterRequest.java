package clyvasync.Clyvasync.dto.record;

public record PolicyFilterRequest(
        Boolean allowsPets,
        Boolean allowsSmoking,
        Boolean allowsParties,
        Boolean allowsChildren,
        Boolean noDeposit
) {}