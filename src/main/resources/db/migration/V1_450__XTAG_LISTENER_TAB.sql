begin
  DBMS_AQADM.CREATE_QUEUE_TABLE (
    queue_table         => 'XTAG.XTAG_LISTENER_TAB',
    queue_payload_type  => 'SYS.AQ$_JMS_MESSAGE');

  DBMS_AQADM.CREATE_QUEUE (
    queue_name          => 'XTAG.XTAG_DPS',
    queue_table         => 'XTAG.XTAG_LISTENER_TAB',
    retention_time      => 86400,
    max_retries         => 3,
    retry_delay         => 1); -- 1s makes testing easier than with immediate retries

  DBMS_AQADM.START_QUEUE (
    queue_name  => 'XTAG.XTAG_DPS');

  DBMS_AQADM.START_QUEUE (
    queue_name  => 'XTAG.AQ$_XTAG_LISTENER_TAB_E',
    enqueue     => false,
    dequeue     => true);

  DBMS_AQADM.GRANT_QUEUE_PRIVILEGE ('ENQUEUE', 'XTAG.XTAG_DPS', 'HMPPS_EVENT_API');
  DBMS_AQADM.GRANT_QUEUE_PRIVILEGE ('DEQUEUE', 'XTAG.XTAG_DPS', 'HMPPS_EVENT_API');
  DBMS_AQADM.GRANT_QUEUE_PRIVILEGE ('DEQUEUE', 'XTAG.AQ$_XTAG_LISTENER_TAB_E', 'HMPPS_EVENT_API');
end;
/

GRANT SELECT ON XTAG.XTAG_LISTENER_TAB TO HMPPS_EVENT_API;
-- Just to support testing:
GRANT DELETE ON XTAG.XTAG_LISTENER_TAB TO HMPPS_EVENT_API;
