# MediaForge

MediaForge is a web application for uploading and optimizing images and videos.

I built this project to practice and demonstrate backend development with Spring Boot, authentication, database management, media processing, and a React frontend.

## What it does

* User registration and login
* JWT-based authentication
* Upload images and videos
* Optimize images using ImageMagick
* Optimize videos using FFmpeg
* Track optimization status
* View media history
* Search and filter uploaded media
* Download optimized files
* Delete media
* Light and dark mode
* User-specific media history

## Tech Stack

### Backend

* Java 21
* Spring Boot
* Spring Security
* JWT
* Spring Data JPA
* MySQL
* ImageMagick
* FFmpeg

### Frontend

* React
* Vite
* Axios
* React Router
* CSS

### Deployment

* Frontend: Vercel
* Backend: Render
* Database: Aiven MySQL

## How it works

1. Create an account or log in.
2. Upload an image or video.
3. MediaForge processes the file using ImageMagick or FFmpeg.
4. The optimization status is updated while processing.
5. Once completed, you can see the original and optimized file sizes.
6. You can download or delete the optimized media.
7. Your media history is linked to your account.

## Live Demo

https://media-forge-sepia.vercel.app/

## GitHub

https://github.com/sonaliacharya13/MediaForge

## Project Status

The main application is working and deployed online.

I am continuing to improve the project, especially around production file storage, performance, and overall user experience.

## What I Learned

While building MediaForge, I worked with:

* Building REST APIs with Spring Boot
* JWT authentication and authorization
* Connecting Spring Boot with MySQL
* Handling file uploads
* Image and video processing
* Asynchronous processing
* User-specific data access
* Building a React frontend
* Connecting frontend and backend
* Deploying a full-stack application

## Future Improvements

* Permanent cloud storage for uploaded media
* Better error handling
* More optimization options
* Improved UI/UX
* Performance improvements
* More deployment and production improvements
