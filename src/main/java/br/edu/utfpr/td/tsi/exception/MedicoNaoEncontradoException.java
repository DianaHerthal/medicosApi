package br.edu.utfpr.td.tsi.exception;

public class MedicoNaoEncontradoException extends RuntimeException {

    public MedicoNaoEncontradoException() {
        super("Médico não encontrado.");
    }

    public MedicoNaoEncontradoException(String id) {
        super("Médico não encontrado com ID: " + id);
    }
}