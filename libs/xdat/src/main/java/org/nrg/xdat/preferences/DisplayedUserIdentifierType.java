package org.nrg.xdat.preferences;

import org.nrg.xft.security.UserI;

public enum DisplayedUserIdentifierType {
    USERNAME {
        @Override
        public String format(UserI user) {
            return user.getUsername();
        }
    },
    EMAIL {
        @Override
        public String format(UserI user) {
            return user.getEmail();
        }
    },
    NAME_FIRST_SPACE_LAST {
        @Override
        public String format(UserI user) {
            return String.format("%s %s", user.getFirstname(), user.getLastname());
        }
    },
    NAME_LAST_COMMA_FIRST {
        @Override
        public String format(UserI user) {
            return String.format("%s, %s", user.getLastname(), user.getFirstname());
        }
    };

    public abstract String format(UserI user);
}
