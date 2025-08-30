package com.sporty.client.openf1;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * OpenF1 Session
 */
@Getter
@Setter
@NoArgsConstructor
public class OpenF1SessionDTO {
    public Integer circuit_key;
    public String  circuit_short_name;
    public String  country_code;
    public Integer country_key;
    public String  country_name;
    public String  date_end;
    public String  date_start;
    public String  gmt_offset;
    public String  location;
    public Integer meeting_key;
    public Integer session_key;
    public String  session_name;
    public String  session_type;
    public Integer year;
}
