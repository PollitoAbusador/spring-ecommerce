package com.curso.ecommerce.controller;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.curso.ecommerce.model.Producto;
import com.curso.ecommerce.model.Usuario;
import com.curso.ecommerce.service.IUsuarioService;
import com.curso.ecommerce.service.ProductoService;
import com.curso.ecommerce.service.UploadFileService;
import com.curso.ecommerce.service.UsuarioServiceImpl;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    private final Logger LOGGER = LoggerFactory.getLogger(ProductoController.class);

    @Autowired
    private ProductoService productoService;

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private UploadFileService upload;

    @GetMapping("")
    public String show(Model model) {
        model.addAttribute("productos", productoService.findAll());
        return "productos/show";
    }
    //Es una anotación de Spring Boot
    //que se usa para mapear peticiones HTTP GET a un método específico.

    @GetMapping("/create")
    public String create() {
        return "productos/create";
    }
    //Metodo de spring save

    @PostMapping("/save")
    public String save(Producto producto, @RequestParam("img") MultipartFile file, HttpSession session) throws IOException {
        LOGGER.info("Este es el objeto producto {}", producto);

        try {
            // Validar que existe el atributo en sesión
            /*
            Object idUsuarioObj = session.getAttribute("idusuario");
            if (idUsuarioObj == null) {
                LOGGER.error("No hay usuario en sesión");
                return "redirect:/login"; // o la página de login
            }

            // Buscar usuario con validación
            Integer idUsuario = Integer.valueOf(idUsuarioObj.toString());
            Optional<Usuario> usuarioOpt = usuarioService.findById(idUsuario);

            if (!usuarioOpt.isPresent()) {
                LOGGER.error("Usuario no encontrado con ID: {}", idUsuario);
                return "redirect:/login";
            }

            Usuario u = usuarioOpt.get();
            producto.setUsuario(u);
            */
            
            // Manejo de imagen
            if (producto.getId() == null) { // cuando se crea un producto
                String nombreImagen = upload.saveImage(file);
                producto.setImagen(nombreImagen);
            }

            productoService.save(producto);
            return "redirect:/productos";

        } catch (NumberFormatException e) {
            LOGGER.error("Error al convertir ID de usuario: ", e);
            return "redirect:/login";
        } catch (Exception e) {
            LOGGER.error("Error al guardar producto: ", e);
            return "redirect:/administrador/productos?error";
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        Producto producto = new Producto();
        Optional<Producto> optionalProducto = productoService.get(id);
        producto = optionalProducto.get();

        LOGGER.info("Producto buscado: {}", producto);
        model.addAttribute("producto", producto);

        return "productos/edit";
    }

    @PostMapping("/update")
    public String update(Producto producto, @RequestParam("img") MultipartFile file) throws IOException {
        Producto p = new Producto();
        p = productoService.get(producto.getId()).get();

        if (file.isEmpty()) { // editamos el producto pero no cambiamos la imagem

            producto.setImagen(p.getImagen());
        } else {// cuando se edita tbn la imagen			
            //eliminar cuando no sea la imagen por defecto
            if (!p.getImagen().equals("default.jpg")) {
                upload.deleteImage(p.getImagen());
            }
            String nombreImagen = upload.saveImage(file);
            producto.setImagen(nombreImagen);
        }
        producto.setUsuario(p.getUsuario());
        productoService.update(producto);
        return "redirect:/productos";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {

        Producto p = new Producto();
        p = productoService.get(id).get();

        //eliminar cuando no sea la imagen por defecto
        if (!p.getImagen().equals("default.jpg")) {
            upload.deleteImage(p.getImagen());
        }

        productoService.delete(id);
        return "redirect:/productos";
    }

}
