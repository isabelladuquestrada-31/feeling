CREATE DATABASE IF NOT EXISTS feeling;
USE feeling;

create table usuario (
id int auto_increment primary key,
nombre_usuario varchar(50) not null unique,
contrasena varchar(100) not null,
rol varchar(20) not null,
activo boolean not null default true,
fecha_registro datetime not null default current_timestamp
);

create table materia (
codigo varchar(20) primary key,
nombre varchar (100) not null 
);

create table perfil (
usuario_id int primary key,
nombre_preferido varchar(50),
pronombres varchar(30),
whatsapp varchar(20) NOT NULL,
FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

create table preferencias_accesibilidad (
usuario_id int primary key,
letra_grande boolean not null default false,
alto_contraste boolean not null default false,
foreign key (usuario_id) references usuario(id)
);

create table sesion (
id int auto_increment primary key,
usuario_id int not null,
token varchar(255) not null unique,
fecha_expiracion datetime not null,
foreign key (usuario_id) references usuario(id)
);

create table estudiante (
usuario_id int primary key,
programa_academico varchar(100) not null,
foreign key (usuario_id) references usuario(id)
);

create table voluntario (
id int auto_increment primary key,
estudiante_id int not null unique,
estado varchar(20) not null default 'ACTIVO',
foreign key (estudiante_id) references estudiante(usuario_id)
);

create table disponibilidad (
id int auto_increment primary key,
voluntario_id int not null,
dia varchar(10) not null,
hora_inicio time not null,
hora_fin time not null,
foreign key (voluntario_id) references voluntario(id)
);

create table voluntario_tipo_apoyo (
voluntario_id int,
tipo_apoyo varchar(30),
primary key (voluntario_id, tipo_apoyo),
foreign key (voluntario_id) references voluntario(id)
);

create table voluntario_materia (
voluntario_id int,
materia_codigo varchar(20),
primary key (voluntario_id, materia_codigo),
foreign key (voluntario_id) references voluntario(id),
foreign key (materia_codigo) references materia(codigo)
);

create table suspension (
id int auto_increment primary key,
voluntario_id int not null,
administrador_id int not null,
motivo varchar(255) not null,
fecha_inicio datetime not null default current_timestamp,
fecha_fin datetime,
foreign key (voluntario_id) references voluntario(id),
foreign key (administrador_id) references usuario(id)
);

create table solicitud (
id int auto_increment primary key,
estudiante_id int not null,
voluntario_id int not null,
materia_codigo varchar(20) not null,
tema varchar(150) not null,
tipo varchar(30) not null,
modalidad varchar(20) not null,
estado varchar(20) not null default 'PENDIENTE',
fecha_creacion datetime not null default current_timestamp,
fecha_programada datetime not null,
foreign key (estudiante_id) references estudiante(usuario_id),
foreign key (voluntario_id) references voluntario(id),
foreign key (materia_codigo) references materia(codigo)
);

create table ajuste_razonable (
id int auto_increment primary key,
solicitud_id int not null,
tipo varchar(30) not null,
detalle varchar(255),
foreign key (solicitud_id) references solicitud(id)
);



create table calificacion (
solicitud_id int primary key,
puntuacion int not null check (puntuacion between 1 and 5),
comentario varchar(255),
fecha datetime not null default current_timestamp,
foreign key (solicitud_id) references solicitud(id)
);

use feeling;
alter table usuario add programa_academico varchar(100) after rol;

