package com.example.practica_3;

public class Entrada {

    public final String titular;
    public final String autor;
    public final String link;

    public Entrada(String titular, String autor, String link){
        this.titular = titular;
        this.autor = autor;
        this.link = link;
    }

    public String getTitular() {
        return titular;
    }

    public String getAutor() {
        return autor;
    }

    public String getLink() {
        return link;
    }
}
