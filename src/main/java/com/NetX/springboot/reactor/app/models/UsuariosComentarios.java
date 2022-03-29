package com.NetX.springboot.reactor.app.models;

public class UsuariosComentarios {
	
	private Usuario usuario;
	private Comentarios comentario;

	public UsuariosComentarios(Usuario usuario, Comentarios comentario) {
		this.usuario = usuario;
		this.comentario = comentario;
	}

	@Override
	public String toString() {
		return "UsuariosComentarios [usuario=" + usuario + ", comentario=" + comentario + "]";
	}
	
	
}
