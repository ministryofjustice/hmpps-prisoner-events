@file:Suppress("PropertyName")

package uk.gov.justice.digital.hmpps.prisonerevents.service.xtag

class XtagContent(private val map: Map<String, String>) {
  private val m = map.withDefault { null }
  val p_offender_id by m
  val p_root_offender_id by m
  val p_offender_book_id by m
  val p_offender_id_display by m
  val p_address_usage by m
  val p_address_end_date by m
  val p_address_deleted by m
  val p_owner_class by m
  val p_primary_addr_flag by m
  val p_mail_addr_flag by m
  val p_delete_flag by m
  val p_old_prison_num by m
  val p_new_prison_num by m
  val p_identifier_value by m
  val p_alert_seq by m
  val p_assessment_seq by m
  val p_assessment_type by m
  val p_review_level_sup_type by m
  val p_evaluation_result_code by m
  val p_imprison_status_seq by m
  val p_sentence_seq by m
  val p_term_seq by m
  val p_result_seq by m
  val p_offender_charge_id by m
  val p_has_offence_code_changed by m
  val p_charge_seq by m
  val p_sanction_seq by m
  val p_movement_seq by m
  val p_owner_id by m
  val p_person_id by m
  val p_contact_root_offender_id by m
  val p_alias_offender_id by m
  val p_address_id by m
  val p_old_offender_id by m
  val p_offender_sent_calculation_id by m
  val p_oic_hearing_id by m
  val p_agency_incident_id by m
  val p_oic_offence_id by m
  val p_offender_risk_predictor_id by m
  val p_from_agy_loc_id by m
  val p_to_agy_loc_id by m
  val p_plea_finding_code by m
  val p_finding_code by m
  val p_condition_code by m
  val p_alert_code by m
  val p_movement_reason_code by m
  val p_direction_code by m
  val p_escort_code by m
  val p_event_date by m
  val p_alert_date by m
  val p_alert_time by m
  val p_expiry_date by m
  val p_expiry_time by m
  val p_movement_date by m
  val p_movement_time by m
  val p_incident_case_id by m
  val p_party_seq by m
  val p_requirement_seq by m
  val p_question_seq by m
  val p_response_seq by m
  val p_table_name by m
  val p_alert_type by m
  val p_movement_type by m
  val p_identifier_type by m
  val p_identifier_no by m
  val p_bed_assign_seq by m
  val p_living_unit_id by m
  val p_record_deleted by m
  val p_event_id by m
  val p_start_time by m
  val p_end_time by m
  val p_event_class by m
  val p_event_type by m
  val p_event_sub_type by m
  val p_event_status by m
  val p_agy_loc_id by m

  val p_iep_level_seq by m
  val p_iep_level by m

  val p_offender_visit_id by m
  val p_audit_module_name by m

  val p_case_note_id by m
  val p_case_note_type by m
  val p_case_note_sub_type by m

  val p_csip_id by m
  val p_attendee_id by m
  val p_csip_factor_id by m
  val p_csip_intvw_id by m
  val p_plan_id by m
  val p_review_id by m

  val p_document_id by m
  val p_document_name by m
  val p_template_id by m
  val p_template_name by m

  val p_offender_key_date_adjust_id by m
  val p_offender_sentence_adjust_id by m

  val p_ns_offender_id_display by m
  val p_ns_offender_book_id by m
  val p_ns_reason_code by m
  val p_ns_level_code by m
  val p_ns_type by m
  val p_type_seq by m
  val p_ns_effective_date by m
  val p_ns_expiry_date by m
  val p_authorized_staff by m

  val p_offender_restriction_id by m
  val p_restriction_type by m
  val p_effective_date by m
  val p_authorised_staff_id by m
  val p_authorized_staff_id by m
  val p_entered_staff_id by m

  val p_offender_contact_person_id by m
  val p_offender_person_restrict_id by m
  val p_restriction_effective_date by m
  val p_restriction_expiry_date by m

  val p_visit_restriction_type by m
  val p_visitor_restriction_id by m

  val p_action by m
  val p_user by m

  val p_internal_location_id by m
  val p_description by m
  val p_old_description by m
  val p_usage_location_id by m

  val p_nomis_timestamp by m

  val p_case_id by m
  val p_order_id by m

  val p_phone_id by m
  val p_phone_type by m

  val p_internet_address_class by m
  val p_internet_address_id by m
  val p_approved_visitor_flag by m
  val p_employment_seq by m
  val p_id_seq by m

  val p_profile_type by m
  val p_corporate_id by m

  val p_id_mark_seq by m

  val p_offender_image_id by m
  val p_image_object_type by m
  val p_full_size_image_changed by m
  val p_active_flag_changed by m
  override fun toString(): String = map.toString()
}
