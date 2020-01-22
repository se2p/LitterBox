/*
 * Copyright (C) 2019 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox.analytics;

import java.util.List;

/**
 * Wrapper class that simulates a issue and holds different information
 */
public class IssueReport {

    private String name;
    private int count;
    private List<String> position;
    private String notes;

    /**
     * @param count    How often the IssueReport appears
     * @param position [0] = stage, [1],[2],... = sprites
     * @param notes    Notes defined by each IssueFinder
     */
    public IssueReport(String name, int count, List<String> position, String notes) {
        this.name = name;
        this.count = count;
        this.position = position;

        this.notes = notes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Issue ").append(name).append(" was found ").append(count).append(" time(s).");
        if (position != null && position.size() > 0) {
            sb.append("\nPosition: ").append(position);
        }
        sb.append("\nNotes: ").append(notes);
        sb.append("\n--------------------------------------------");
        return sb.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<String> getPosition() {
        return position;
    }

    public void setPosition(List<String> position) {
        this.position = position;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
