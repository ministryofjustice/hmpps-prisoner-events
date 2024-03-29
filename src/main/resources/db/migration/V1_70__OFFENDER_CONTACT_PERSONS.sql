CREATE TABLE "OFFENDER_CONTACT_PERSONS"
(
  "OFFENDER_BOOK_ID"              NUMBER(10, 0)                     NOT NULL ,
  "PERSON_ID"                     NUMBER(10, 0),
  "CONTACT_TYPE"                  VARCHAR2(12 CHAR)                 NOT NULL ,
  "RELATIONSHIP_TYPE"             VARCHAR2(12 CHAR)                 NOT NULL ,
  "APPROVED_VISITOR_FLAG"         VARCHAR2(1 CHAR) DEFAULT 'N',
  "CASELOAD_TYPE"                 VARCHAR2(12 CHAR),
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "COMMENT_TEXT"                  VARCHAR2(240 CHAR),
  "CASE_INFO_NUMBER"              VARCHAR2(60 CHAR),
  "AWARE_OF_CHARGES_FLAG"         VARCHAR2(1 CHAR) DEFAULT 'N',
  "CAN_BE_CONTACTED_FLAG"         VARCHAR2(1 CHAR) DEFAULT 'N',
  "CREATE_DATETIME"               TIMESTAMP(9) DEFAULT systimestamp NOT NULL ,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT USER    NOT NULL ,
  "EMERGENCY_CONTACT_FLAG"        VARCHAR2(1 CHAR) DEFAULT 'N'      NOT NULL ,
  "NEXT_OF_KIN_FLAG"              VARCHAR2(1 CHAR) DEFAULT 'N'      NOT NULL ,
  "ACTIVE_FLAG"                   VARCHAR2(1 CHAR) DEFAULT 'N'      NOT NULL ,
  "EXPIRY_DATE"                   DATE,
  "OFFENDER_CONTACT_PERSON_ID"    NUMBER(10, 0)                     NOT NULL ,
  "CONTACT_ROOT_OFFENDER_ID"      NUMBER(10, 0),
  "AUDIT_TIMESTAMP"               TIMESTAMP(9),
  "AUDIT_USER_ID"                 VARCHAR2(32 CHAR),
  "AUDIT_MODULE_NAME"             VARCHAR2(65 CHAR),
  "AUDIT_CLIENT_USER_ID"          VARCHAR2(64 CHAR),
  "AUDIT_CLIENT_IP_ADDRESS"       VARCHAR2(39 CHAR),
  "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64 CHAR),
  "AUDIT_ADDITIONAL_INFO"         VARCHAR2(256 CHAR),

  CONSTRAINT "OFFENDER_CONTACT_PERSONS_PK" PRIMARY KEY ("OFFENDER_CONTACT_PERSON_ID"),

  CONSTRAINT "OFFENDER_CONTACT_PERSONS_UK1" UNIQUE ("OFFENDER_BOOK_ID", "CONTACT_TYPE", "RELATIONSHIP_TYPE", "PERSON_ID", "CONTACT_ROOT_OFFENDER_ID"),

  CONSTRAINT "OFF_CP_OFF_BKG_F1"              FOREIGN KEY ("OFFENDER_BOOK_ID")                  REFERENCES "OFFENDER_BOOKINGS" ("OFFENDER_BOOK_ID")
  --, CONSTRAINT "OFF_CP_CONT_PT_F1"              FOREIGN KEY ("CONTACT_TYPE", "RELATIONSHIP_TYPE") REFERENCES "CONTACT_PERSON_TYPES" ("CONTACT_TYPE", "RELATIONSHIP_TYPE") ,
  -- CONSTRAINT "OFF_CONTACT_PERSONS_PERSONS_FK" FOREIGN KEY ("PERSON_ID")                         REFERENCES "PERSONS" ("PERSON_ID")
);


COMMENT ON COLUMN "OFFENDER_CONTACT_PERSONS"."OFFENDER_BOOK_ID" IS 'System generated identifier for an offender booking.';

COMMENT ON COLUMN "OFFENDER_CONTACT_PERSONS"."PERSON_ID" IS 'System generated identider for a person.';

COMMENT ON COLUMN "OFFENDER_CONTACT_PERSONS"."CONTACT_TYPE" IS 'The contact type with offender ie. Emergency, Professional..';

COMMENT ON COLUMN "OFFENDER_CONTACT_PERSONS"."RELATIONSHIP_TYPE" IS 'The relationship with offender ie. Friend, Wife, Brother..';

COMMENT ON COLUMN "OFFENDER_CONTACT_PERSONS"."APPROVED_VISITOR_FLAG" IS 'Is this person an approved visitor?';

COMMENT ON COLUMN "OFFENDER_CONTACT_PERSONS"."CASELOAD_TYPE" IS 'Caseload Type';

COMMENT ON COLUMN "OFFENDER_CONTACT_PERSONS"."MODIFY_DATETIME" IS 'The timestamp when the record is modified ';

COMMENT ON COLUMN "OFFENDER_CONTACT_PERSONS"."MODIFY_USER_ID" IS 'The user who modifies the record';

COMMENT ON COLUMN "OFFENDER_CONTACT_PERSONS"."COMMENT_TEXT" IS 'Comment Text';

COMMENT ON COLUMN "OFFENDER_CONTACT_PERSONS"."CASE_INFO_NUMBER" IS 'Case Info Number';

COMMENT ON COLUMN "OFFENDER_CONTACT_PERSONS"."AWARE_OF_CHARGES_FLAG" IS 'Is the person aware of the charges';

COMMENT ON COLUMN "OFFENDER_CONTACT_PERSONS"."CAN_BE_CONTACTED_FLAG" IS 'Can be contacted ?';

COMMENT ON COLUMN "OFFENDER_CONTACT_PERSONS"."CREATE_DATETIME" IS 'The timestamp when the record is created';

COMMENT ON COLUMN "OFFENDER_CONTACT_PERSONS"."CREATE_USER_ID" IS 'The user who creates the record';

COMMENT ON COLUMN "OFFENDER_CONTACT_PERSONS"."EMERGENCY_CONTACT_FLAG" IS 'Is the person emergency contact';

COMMENT ON COLUMN "OFFENDER_CONTACT_PERSONS"."NEXT_OF_KIN_FLAG" IS 'Is the person next of Kin';

COMMENT ON COLUMN "OFFENDER_CONTACT_PERSONS"."ACTIVE_FLAG" IS 'Is it a active record';

COMMENT ON COLUMN "OFFENDER_CONTACT_PERSONS"."EXPIRY_DATE" IS 'Expiry date for the data';

COMMENT ON TABLE "OFFENDER_CONTACT_PERSONS" IS 'Contact person for offender';


CREATE INDEX "OFFENDER_CONTACT_PERSONS_NI1" ON "OFFENDER_CONTACT_PERSONS" ("PERSON_ID");

CREATE INDEX "OFFENDER_CONTACT_PERSONS_NI2" ON "OFFENDER_CONTACT_PERSONS" ("OFFENDER_BOOK_ID");

CREATE INDEX "OFFENDER_CONTACT_PERSONS_NI3" ON "OFFENDER_CONTACT_PERSONS" ("CONTACT_ROOT_OFFENDER_ID");

CREATE INDEX "OFF_CP_CONT_PT_F1" ON "OFFENDER_CONTACT_PERSONS" ("CONTACT_TYPE", "RELATIONSHIP_TYPE");

GRANT SELECT ON OFFENDER_CONTACT_PERSONS TO HMPPS_EVENT_API;
GRANT INSERT ON OFFENDER_CONTACT_PERSONS TO HMPPS_EVENT_API;
GRANT DELETE ON OFFENDER_CONTACT_PERSONS TO HMPPS_EVENT_API;
CREATE PUBLIC SYNONYM OFFENDER_CONTACT_PERSONS FOR SYS.OFFENDER_CONTACT_PERSONS;
