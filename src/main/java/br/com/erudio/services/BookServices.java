package br.com.erudio.services;

import br.com.erudio.controllers.BookController;
import br.com.erudio.data.dto.v1.BookDTOV1;
import br.com.erudio.exception.RequiredObjectIsNullException;
import br.com.erudio.exception.ResourceNotFoundException;
import br.com.erudio.model.Book;
import br.com.erudio.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static br.com.erudio.mapper.ObjectMapper.parseListObjects;
import static br.com.erudio.mapper.ObjectMapper.parseObject;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class BookServices {

    private Logger logger = LoggerFactory.getLogger(BookServices.class.getName());

    @Autowired
    BookRepository repository;

    public List<BookDTOV1> findAll(){
        logger.info("Finding all Books!");
        var books = parseListObjects(repository.findAll(), BookDTOV1.class);
        books.forEach(this::addHateoasLinks);
        return books;
    }

    public BookDTOV1 findById(Long id){
        logger.info("Finding one Book!");
        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this id"));
        var dto = parseObject(entity, BookDTOV1.class);
        addHateoasLinks(dto);
        return dto;
    }

    public BookDTOV1 create(BookDTOV1 book){
        if (book == null) {
            throw new RequiredObjectIsNullException();
        }else {
            logger.info("Creating one Book!");
            var entity = parseObject(book, Book.class);
            var dto = parseObject(repository.save(entity), BookDTOV1.class);
            addHateoasLinks(dto);
            return dto;
        }
    }

    public BookDTOV1 update(BookDTOV1 book){
        logger.info("Updating one Book!");
        if(book == null){
            throw new RequiredObjectIsNullException();
        }else{
            Book entity = repository.findById(book.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("No records found for this id"));
            entity.setAuthor(book.getAuthor());
            entity.setLaunchDate(book.getLaunchDate());
            entity.setPrice(book.getPrice());
            entity.setAuthor(book.getAuthor());
            var dto = parseObject(repository.save(entity), BookDTOV1.class);
            addHateoasLinks(dto);
            return dto;
        }
    }

    public void delete(Long id){
        logger.info("Deleting one Book!");
        Book entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No records found for this id"));
        repository.delete(entity);
    }

    private void addHateoasLinks(BookDTOV1 dto) {
        dto.add(linkTo(methodOn(BookController.class).findByeId(dto.getId())).withSelfRel().withType("GET"));
        dto.add(linkTo(methodOn(BookController.class).findAll()).withRel("findALL").withType("GET"));
        dto.add(linkTo(methodOn(BookController.class).create(dto)).withRel("create").withType("POST"));
        dto.add(linkTo(methodOn(BookController.class).update(dto)).withRel("update").withType("PUT"));
        dto.add(linkTo(methodOn(BookController.class).delete(dto.getId())).withRel("delete").withType("DELETE"));
    }
}
