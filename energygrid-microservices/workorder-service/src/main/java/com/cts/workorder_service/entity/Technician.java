package com.cts.workorder_service.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Denormalised cache of a TECHNICIAN-role user from identity-service.
 *
 * The PK is set explicitly to the identity userId — there is no auto-
 * generation here. The row is created by {@code assignTechnician} the
 * first time a given user is assigned to a work order, so any identity
 * user with role=TECHNICIAN can be assigned without a separate
 * provisioning step in workorder_db. This was previously the cause of
 * the perpetual "Technician {id} not found" errors.
 */
@Entity
@Table(name = "technicians")
@Data
public class Technician {

    @Id
    private Long id;

    private String name;
    private String skill;
}
