package com.awadhesh22791;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SsoDetails {
	@JsonProperty("relying-parties")
	private List<Map<String, String>>relyingParties;
}
