package com.jpinpoint.monitor.demo;

import lombok.NonNull;

public class Greeting {

    private final long id;
    private final String content;

    public Greeting(long id, @NonNull String content) {
        this.id = id;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    @Override
	public String toString() {
		return "Greeting{" + "id=" + id + ", content='" + content + '\'' + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Greeting greeting = (Greeting) o;
		if (id != greeting.id) return false;
		return content.equals(greeting.content);
	}

	@Override
	public int hashCode() {
		int result = (int) (id ^ (id >>> 32));
		result = 31 * result + content.hashCode();
		return result;
	}
}
