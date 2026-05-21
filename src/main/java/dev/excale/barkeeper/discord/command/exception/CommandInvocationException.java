package dev.excale.barkeeper.discord.command.exception;

public class CommandInvocationException extends Exception {

	public CommandInvocationException(String message, Throwable cause) {
		super(message, cause);
	}

}
