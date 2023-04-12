
  CREATE TABLE "OFFENDER_PERSON_RESTRICTS"
   (    "OFFENDER_CONTACT_PERSON_ID" NUMBER(10,0) NOT NULL,
    "OFFENDER_PERSON_RESTRICT_ID" NUMBER(10,0) NOT NULL,
    "RESTRICTION_TYPE" VARCHAR2(12 CHAR) NOT NULL,
    "RESTRICTION_EFFECTIVE_DATE" DATE NOT NULL,
    "RESTRICTION_EXPIRY_DATE" DATE,
    "AUTHORIZED_STAFF_ID" NUMBER(10,0),
    "COMMENT_TEXT" VARCHAR2(255 CHAR),
    "ENTERED_STAFF_ID" NUMBER(10,0),
    "CREATE_DATETIME" TIMESTAMP (9) DEFAULT systimestamp NOT NULL,
    "CREATE_USER_ID" VARCHAR2(32 CHAR) DEFAULT USER NOT NULL,
    "MODIFY_DATETIME" TIMESTAMP (9),
    "MODIFY_USER_ID" VARCHAR2(32 CHAR),
    "AUDIT_TIMESTAMP" TIMESTAMP (9),
    "AUDIT_USER_ID" VARCHAR2(32 CHAR),
    "AUDIT_MODULE_NAME" VARCHAR2(65 CHAR),
    "AUDIT_CLIENT_USER_ID" VARCHAR2(64 CHAR),
    "AUDIT_CLIENT_IP_ADDRESS" VARCHAR2(39 CHAR),
    "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64 CHAR),
    "AUDIT_ADDITIONAL_INFO" VARCHAR2(256 CHAR),
     CONSTRAINT "OFFENDER_PERSON_RESTRICTS_PK" PRIMARY KEY ("OFFENDER_PERSON_RESTRICT_ID")
  );

  CREATE INDEX "OFF_PER_REST_STAF_MEM_FK1" ON "OFFENDER_PERSON_RESTRICTS" ("AUTHORIZED_STAFF_ID");

  CREATE INDEX "OFF_PER_REST_STF_FK2" ON "OFFENDER_PERSON_RESTRICTS" ("ENTERED_STAFF_ID");

  CREATE INDEX "OFFENDER_PERSON_RESTRICTS_NI1" ON "OFFENDER_PERSON_RESTRICTS" ("OFFENDER_CONTACT_PERSON_ID");

GRANT SELECT ON OFFENDER_PERSON_RESTRICTS TO HMPPS_EVENT_API;
GRANT INSERT ON OFFENDER_PERSON_RESTRICTS TO HMPPS_EVENT_API;
GRANT DELETE ON OFFENDER_PERSON_RESTRICTS TO HMPPS_EVENT_API;
CREATE PUBLIC SYNONYM OFFENDER_PERSON_RESTRICTS FOR SYS.OFFENDER_PERSON_RESTRICTS;
