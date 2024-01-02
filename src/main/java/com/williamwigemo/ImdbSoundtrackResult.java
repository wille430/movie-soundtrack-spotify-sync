package com.williamwigemo;

public class ImdbSoundtrackResult {
    private String title;

    public static class Collaborators {
        private String writtenBy;
        private String performedBy;
        private String composedBy;

        public String getWrittenBy() {
            return writtenBy;
        }

        public void setWrittenBy(String writtenBy) {
            this.writtenBy = writtenBy;
        }

        public String getPerformedBy() {
            return performedBy;
        }

        public void setPerformedBy(String performedBy) {
            this.performedBy = performedBy;
        }

        public String getComposedBy() {
            return composedBy;
        }

        public void setComposedBy(String composedBy) {
            this.composedBy = composedBy;
        }

        public String getPrimaryCollaborator() {
            return performedBy != null ? performedBy
                    : writtenBy != null ? writtenBy : composedBy != null ? composedBy : null;
        }
    }

    private Collaborators collaborators;

    public ImdbSoundtrackResult(String title, Collaborators collaborators) {
        this.title = title;
        this.collaborators = collaborators;
    }

    public String getTitle() {
        return title;
    }

    public Collaborators getCollaborators() {
        return collaborators;
    }
}
