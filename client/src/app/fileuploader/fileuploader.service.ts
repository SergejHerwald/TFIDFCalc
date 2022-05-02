import { Injectable } from '@angular/core';
import {Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class FileuploaderService {

  constructor(private http: HttpClient) { }

  apiPath:string = "http://localhost:8080"

  upload(file:File):Observable<{uploaded: boolean}> {
    const formData: FormData = new FormData();
    formData.append('file', file);
    return this.http.post<{uploaded: boolean}>(`${this.apiPath}/api/uploadFile`, formData)
  }

  getFiles():Observable<string[]> {
    return this.http.get<string[]>(`${this.apiPath}/api/getFiles`)
  }

  startBatchWork():Observable<any> {
    return this.http.get<any>(`${this.apiPath}/api/startBatchWork`)
  }
}
