package com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Work Item Attribute
 * Used to generate the work item mapping inforrmation sent to the UI
 */
public class AttributeDefinition {
	private String identifier;
	private String displayName;
	private List<MappingDefinition> valueMappings;
	private boolean isPrimitive;

	public AttributeDefinition(String id, boolean isPrimitive) {
		this.identifier = id;
		this.isPrimitive = isPrimitive;
		valueMappings = new ArrayList<MappingDefinition>();
	}

	public AttributeDefinition(String id, String name, boolean isPrimitive) {
		this.identifier = id;
		this.displayName = name;
		this.isPrimitive = isPrimitive;
		valueMappings = new ArrayList<MappingDefinition>();
	}
	
	public String getIdentifier() {
		return identifier;
	}

	public List<MappingDefinition> getMappingDefinitions() {
		return valueMappings;
	}
	
	public void addValueMapping(MappingDefinition mapping) {
		valueMappings.add(mapping);
	}

	public MappingDefinition getMapping(AttributeValue oldValueIdentifier) {
		if(oldValueIdentifier != null) {
			for (MappingDefinition map : valueMappings) {
				if(map.getOldValue() != null) {
					if (!isPrimitive && oldValueIdentifier.getIdentifier() != null && oldValueIdentifier.getIdentifier().equals(map.getOldValue().getIdentifier())) {
						return map;
					} else if (isPrimitive && oldValueIdentifier.getDisplayName() != null && oldValueIdentifier.getDisplayName().equals(map.getOldValue().getDisplayName())) {
						return map;
					}
				}
			}
		}
		return null;
	}

    /**
     * override equals to compare by unique attribute identifier
     * @param object AttributeDefinition object to be compared
     * @return true if both share the same attribute identifier
     */
	@Override
	public boolean equals(Object object) {
	    if(object instanceof AttributeDefinition) {
	    	AttributeDefinition el = (AttributeDefinition)object;
	    	return this.identifier.equals(el.getIdentifier());
	    } else {
	        return false;
	    }
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hashCode(identifier);
	}
}