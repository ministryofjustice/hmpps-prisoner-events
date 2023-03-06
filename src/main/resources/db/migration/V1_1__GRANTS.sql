CREATE USER XTAG IDENTIFIED BY test;
CREATE USER HMPPS_EVENT_API IDENTIFIED BY test;

grant create SESSION to XTAG;
grant create SESSION to HMPPS_EVENT_API;
GRANT EXECUTE ON DBMS_AQADM TO XTAG;
GRANT CREATE EVALUATION CONTEXT TO XTAG;
GRANT CREATE RULE TO XTAG;
GRANT CREATE RULE SET TO XTAG;
GRANT DEQUEUE ANY QUEUE TO XTAG;
GRANT ENQUEUE ANY QUEUE TO XTAG;
GRANT MANAGE ANY QUEUE TO XTAG;
GRANT CREATE PROCEDURE TO XTAG;
GRANT CREATE SESSION TO XTAG;
GRANT CREATE TABLE TO XTAG;
GRANT CREATE TYPE TO XTAG;
GRANT UNLIMITED TABLESPACE TO XTAG;

BEGIN
   DBMS_AQADM.GRANT_SYSTEM_PRIVILEGE(
      privilege          =>    'MANAGE_ANY',
      grantee            =>    'XTAG',
      admin_option       =>     FALSE);
END;
/

GRANT EXECUTE ON DBMS_AQ TO HMPPS_EVENT_API;
GRANT EXECUTE ON DBMS_AQIN to HMPPS_EVENT_API;
GRANT EXECUTE ON SYS.DBMS_MONITOR TO HMPPS_EVENT_API;
GRANT CONNECT TO HMPPS_EVENT_API;
