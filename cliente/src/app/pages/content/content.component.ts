import { Component, OnInit, ViewChild } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTable, MatTableModule } from '@angular/material/table';
import { KeycloakService } from 'keycloak-angular';
import { Movie, MovieBackendService } from '../../services/MovieBackendService';
import { MatTabsModule } from '@angular/material/tabs';
import { MatOptionModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { MatSortModule } from '@angular/material/sort';

@Component({
  selector: 'app-content',
  standalone: true,
  templateUrl: './content.component.html',
  imports: [MatTabsModule, MatOptionModule, MatSelectModule, MatSortModule, MatTableModule],
  styleUrls: ['./content.component.scss']
})
export class ContentComponent {

  movies: Movie[] = []
  displayedColumns: string[] = ['title', 'director', 'year']; 
  
  @ViewChild(MatTable) table!: MatTable<any>;

  constructor(
    private keycloakService: KeycloakService,
    private backend: MovieBackendService,
    private snackBar: MatSnackBar) {
    
  }

  logout() {
    this.keycloakService.logout();
  }

  getAllMovies() {
    this.backend.getAllMovies().subscribe(
        
        response => {
          this.movies = response
          this.table.renderRows();          
        },

        error => {
          this.handleError(error.error)
        })
  }

  onMovieIdChange(event: any){
    this.getMovieById(event.value);
  }

  private getMovieById(id: number) {
    this.backend.getMovieById(id).subscribe(
          
        response => {
          this.movies = [response]
          this.table.renderRows();
        },
        
        error => {
          this.handleError(error.error)
        })
  }

  private handleError(error: any) {
    this.displayError(error.code + ' ' + error.reason + ". " + error.message)
  }

  private displayError(message: string) {
    this.snackBar.open(message, 'Close', { duration: 5000})
  }

}