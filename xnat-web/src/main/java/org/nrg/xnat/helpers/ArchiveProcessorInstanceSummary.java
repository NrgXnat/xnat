
package org.nrg.xnat.helpers;

import org.nrg.xnat.entities.ArchiveProcessorInstance;

public class ArchiveProcessorInstanceSummary {
    public ArchiveProcessorInstanceSummary(ArchiveProcessorInstance instance){
        this.label = instance.getLabel();
        this.priority = instance.getPriority();
        this.location = instance.getLocation();
        this.processorClass = instance.getProcessorClass();
    }

    @Override
    public String toString() {
        return "ArchiveProcessorInstanceSummary{" +
                "label='" + label + '\'' +
                ", location=" + location +
                ", priority=" + priority +
                ", processorClass='" + processorClass + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArchiveProcessorInstanceSummary that = (ArchiveProcessorInstanceSummary) o;

        if (location != that.location) return false;
        if (priority != that.priority) return false;
        if (label != null ? !label.equals(that.label) : that.label != null) return false;
        return processorClass != null ? processorClass.equals(that.processorClass) : that.processorClass == null;
    }

    @Override
    public int hashCode() {
        int result = label != null ? label.hashCode() : 0;
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + priority;
        result = 31 * result + (processorClass != null ? processorClass.hashCode() : 0);
        return result;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getProcessorClass() {
        return processorClass;
    }

    public void setProcessorClass(String processorClass) {
        this.processorClass = processorClass;
    }

    private String label;
    private String location;
    private int priority;
    private String processorClass;
}